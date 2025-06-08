package pe.com.prueba.plataformacontrolcomercio.mapper;

import org.springframework.stereotype.Component;
import pe.com.prueba.plataformacontrolcomercio.dto.ProductDTO;
import pe.com.prueba.plataformacontrolcomercio.model.Product;
import pe.com.prueba.plataformacontrolcomercio.repository.CategoryRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.ProducerRepository;
import pe.com.prueba.plataformacontrolcomercio.repository.TagRepository;

@Component
public class ProductMapper
{
    private final ProducerRepository producerRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public ProductMapper(ProducerRepository producerRepository,
            CategoryRepository categoryRepository, TagRepository tagRepository)
    {
        this.producerRepository = producerRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    public ProductDTO toDTO(Product product)
    {
        return new ProductDTO(product);
    }

    public Product toEntity(ProductDTO dto)
    {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        product.setQuantity(dto.getQuantity());
        product.setSku(dto.getSku());
        product.setCreatedAt(dto.getCreatedAt());
        product.setUpdatedAt(dto.getUpdatedAt());

        if (dto.getProducerId() != null)
        {
            product.setProducer(producerRepository.findById(dto.getProducerId())
                    .orElse(null));
        }

        product.getProductCategories().clear();
        if (dto.getCategoryIds() != null)
        {
            dto.getCategoryIds().forEach(categoryId -> {
                categoryRepository.findById(categoryId)
                        .ifPresent(category -> product.addCategory(category));
            });
        }

        // Limpiar y añadir etiquetas
        product.getProductTags().clear();
        if (dto.getTagIds() != null)
        {
            dto.getTagIds().forEach(tagId -> {
                tagRepository.findById(tagId)
                        .ifPresent(tag -> product.addTag(tag));
            });
        }

        return product;
    }
}