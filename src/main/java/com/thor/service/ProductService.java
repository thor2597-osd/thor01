package com.thor.service;

import com.thor.entity.ProductDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.thor.model.Product;
import com.thor.model.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author OSD
 * @since 2024-11-01
 */
public interface ProductService extends IService<ProductDO> {

    Result queryBrandAndChildrenNumber();

    Result delete(String brand);

    Result add(ProductDO productDO);

    Result updateProduct(ProductDO productDO);

    Product addProductDetail(String brand);

    void decProductDetail(long id);

    Product selectProductById(long id);
}
