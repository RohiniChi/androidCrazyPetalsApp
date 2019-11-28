package com.plugable.mcommerceapp.cpmvp1.mcommerce.db

import androidx.room.*
import com.plugable.mcommerceapp.cpmvp1.mcommerce.models.Products


/**
 * [FavoriteProductListDao] is an dao interface to handle db queries
 *
 */
@Dao
interface FavoriteProductListDao {

    @Query("SELECT * FROM productWishList")
    fun getAllWishListedProducts(): List<Products.Data.ProductDetails>

    @Query("SELECT count(*) FROM productWishList")
    fun getTotalCount(): Int

    @Query("Select * from productWishList where id=:productId")
    fun getSingleWishListProduct(productId: Int): Products.Data.ProductDetails


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllProducts(vararg products: Products.Data.ProductDetails)

    @Delete
    fun deleteProduct(product: Products.Data.ProductDetails)

}