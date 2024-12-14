package com.programmermuda.crud.controllers;

import com.programmermuda.crud.models.Product;
import com.programmermuda.crud.models.ProductDTO;
import com.programmermuda.crud.services.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"", "/"})
    public String showProduction(Model model){
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDTO productDTO = new ProductDTO();
        model.addAttribute("productDTO", productDTO);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDTO productDTO,
                                BindingResult result){
        if (productDTO.getImageFile().isEmpty()){
            result.addError(new FieldError("productDTO", "imageFile", "The image file is required"));
        }

        if (result.hasErrors()){
            return "products/CreateProduct";
        }

        MultipartFile image = productDTO.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            java.nio.file.Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists( uploadPath)){
                Files.createDirectory( uploadPath);
            }

            try(InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (Exception e){
            System.out.println("Exception : " + e.getMessage());
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setDescription(productDTO.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        productRepository.save(product);

        return "redirect:/products";
    }


    @GetMapping("/edit")
    public String showEditPage(Model model,
                               @RequestParam int id){

        try {
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            ProductDTO productDTO = new ProductDTO();
            product.setName(product.getName());
            product.setBrand(product.getBrand());
            product.setCategory(product.getCategory());
            product.setPrice(product.getPrice());
            product.setDescription(product.getDescription());

            model.addAttribute("productDTO", productDTO);
        }catch (Exception e){
            System.out.println("Exception : " + e.getMessage());
            return "redirect:/products";
        }

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model,
                                @RequestParam int id,
                                @Valid @ModelAttribute ProductDTO productDTO,
                                BindingResult result){

        try {
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);
            if (result.hasErrors()){
                return "products/EditProduct";
            }

            if (!productDTO.getImageFile().isEmpty()){
                // delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                }catch (Exception e){
                    System.out.println("Exception : " + e.getMessage());
                }

                // save new image file
                MultipartFile image = productDTO.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try(InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }
            product.setName(productDTO.getName());
            product.setBrand(productDTO.getBrand());
            product.setCategory(productDTO.getCategory());
            product.setDescription(productDTO.getDescription());

            productRepository.save(product);
        }catch (Exception e){
            System.out.println("Exception : " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id){

        try {
            Product product = productRepository.findById(id).get();

            // delete product image
            Path imagePaath = Paths.get("public/images/" + product.getImageFileName());

            try {
                Files.delete(imagePaath);
            }catch (Exception e){
                System.out.println("Exception : " + e.getMessage());
            }

            //delete the product
            productRepository.delete(product);
        }catch (Exception e){
            System.out.println("Exception : " + e.getMessage());
        }

        return "redirect:/products";
    }

}
