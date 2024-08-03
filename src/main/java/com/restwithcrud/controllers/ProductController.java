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

import java.util.List;

@Controller
public class ProductController {

    private final ProductRepository productRepository;
    private final RestWithCrudApplication restWithCrudApplication;

    public ProductController(ProductRepository productRepository, RestWithCrudApplication restWithCrudApplication) {
        this.productRepository = productRepository;
        this.restWithCrudApplication = restWithCrudApplication;
    }

    @GetMapping({"","/"})
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
        if(productDto.getImageFile().isEmpty()){
            bindingResult.addError(new FieldError("productDto", "imageFile", "Image file is required"));
        }

        if(bindingResult.hasErrors()){
            return "/products/CreateProduct";
        }
        return "redirect:/products";
    }

}
