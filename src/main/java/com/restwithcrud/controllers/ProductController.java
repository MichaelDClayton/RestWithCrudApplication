package com.restwithcrud.controllers;

import com.restwithcrud.RestWithCrudApplication;
import com.restwithcrud.models.Product;
import com.restwithcrud.models.ProductDto;
import com.restwithcrud.services.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;


import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final RestWithCrudApplication restWithCrudApplication;

    public ProductController(ProductRepository productRepository, RestWithCrudApplication restWithCrudApplication) {
        this.productRepository = productRepository;
        this.restWithCrudApplication = restWithCrudApplication;
    }

    @GetMapping({"", "/"})
    public String showProducts(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "/products/index";
    }

    @GetMapping("/create")
    public String showCreateProductForm(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "/products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult bindingResult) {
        if (productDto.getImageFile().isEmpty()) {
            bindingResult.addError(new FieldError("productDto", "imageFile", "Image file is required"));
        }

        if (bindingResult.hasErrors()) {
            return "/products/CreateProduct";
        }

        //save image file
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFilleName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFilleName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setBrand(productDto.getBrand());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFilleName);

        productRepository.save(product);

        return "redirect:/";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Optional<Product> product = productRepository.findById(id);
            model.addAttribute("product", product.get());

            ProductDto productDto = new ProductDto();
            productDto.setName(product.get().getName());
            productDto.setDescription(product.get().getDescription());
            productDto.setPrice(product.get().getPrice());
            productDto.setCategory(product.get().getCategory());
            productDto.setBrand(product.get().getBrand());
            model.addAttribute("productDto", productDto);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }

        return "/products/EditProduct";

    }


    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) {

        try {
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            if (bindingResult.hasErrors()) {
                return "/products/EditProduct";
            }

            if (!product.getImageFileName().isEmpty()) {
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
                System.out.println("Old Image: " + oldImagePath);
                try {
                    Files.delete(oldImagePath);
                } catch (Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                }

                //save new image file.
                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFilleName = createdAt.getTime() + "_" + image.getOriginalFilename();
                System.out.println("New Image: ");
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFilleName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFilleName);
            }

            product.setName(productDto.getName());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());
            product.setCategory(productDto.getCategory());
            product.setBrand(productDto.getBrand());

            productRepository.save(product);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }


        return "redirect:/";
    }


    @GetMapping("/delete")
    public String deleteProduct(Model model, @RequestParam int id) {

        //delete image file first.
        try {
            Product product = productRepository.findById(id).get();


            String uploadDir = "public/images/";
            Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
            try {
                Files.delete(oldImagePath);
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }

            productRepository.deleteById(id);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        return "redirect:/";
    }
}
