package com.sourav.deliveryapp.Api;

import com.sourav.deliveryapp.Model.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    // login call
    @FormUrlEncoded
    @POST("loginShipper")
    Call<Result> loginShipper(
            @Field("phone") String phone,
            @Field("password") String password);

    // get user call
    @FormUrlEncoded
    @POST("getUser")
    Call<Result> getUser(
            @Field("phone") String phone);

    // update customer call
    @FormUrlEncoded
    @POST("updateUser/{id}")
    Call<Result> updateCustomerInfo(@Path("id") String id,
                                    @Field("username") String username,
                                    @Field("password") String password,
                                    @Field("phone") String phone,
                                    @Field("email") String email,
                                    @Field("address") String address);

    @Multipart
    @POST("updateUserWithImage")
    Call<Result> updateUserWithImage(
            @Part("id") RequestBody id,
            @Part("username") RequestBody username,
            @Part("password") RequestBody password,
            @Part("phone") RequestBody phone,
            @Part("email") RequestBody email,
            @Part("address") RequestBody address,
            @Part MultipartBody.Part file);

    //updating token for User
    @FormUrlEncoded
    @POST("updateUserToken")
    Call<Result> updateUserToken(
            @Field("id") String id,
            @Field("token") String token);

    //Get all assigned order
    @FormUrlEncoded
    @POST("getAllAssignedOrderForShipper")
    Call<Result> getAllAssignedOrderForShipper(
            @Field("id_shipper") String id_shipper
    );

    //Get all shipping order
    @FormUrlEncoded
    @POST("getAllShippingOrderForShipper")
    Call<Result> getAllShippingOrderForShipper(
            @Field("id_shipper") String id_shipper
    );

    //Get all Completed order
    @FormUrlEncoded
    @POST("getAllCompletedOrderForShipper")
    Call<Result> getAllCompletedOrderForShipper(
            @Field("id_shipper") String id_shipper
    );

    //Get all Cancelled order
    @FormUrlEncoded
    @POST("getAllCancelledOrderForShipper")
    Call<Result> getAllCancelledOrderForShipper(
            @Field("id_shipper") String id_shipper
    );

    //Get all Completed order based on date
    @FormUrlEncoded
    @POST("getAllCompletedOrderForShipperDate")
    Call<Result> getAllCompletedOrderForShipperDate(
            @Field("id_shipper") String id_shipper,
            @Field("delivery_date") String delivery_date
    );

    //Get all Completed order based on monthly date
    @FormUrlEncoded
    @POST("getAllCompletedOrderForShipperMonth")
    Call<Result> getAllCompletedOrderForShipperMonth(
            @Field("id_shipper") String id_shipper,
            @Field("year") String year,
            @Field("month") String month
    );

    //Update order Status --> ADMIN PART
    @FormUrlEncoded
    @POST("updateOrderStatus")
    Call<Result> updateOrderStatus(
            @Field("id_order") String id_order,
            @Field("id_shipper") String id_shipper,
            @Field("order_status") String order_status,
            @Field("payment_state") String payment_state,
            @Field("food_delivery_date") String food_delivery_date);

    //Get User Token
    @FormUrlEncoded
    @POST("getUserToken")
    Call<Result> getUserToken(
            @Field("id") String id,
            @Field("isServerToken") String isServerToken);

    //get order details
    @FormUrlEncoded
    @POST("getOrderDetails")
    Call<Result> getOrderDetails(@Field("id_order") String id_order);


    //Get order Details for Admin
    @FormUrlEncoded
    @POST("getOrderItemsAdmin")
    Call<Result> getOrderItemsAdmin(
            @Field("id_order") String id_order);

    //Transfer wallet amount to ADMIN
    @FormUrlEncoded
    @POST("addWalletTransfer")
    Call<Result> addWalletTransfer(
            @Field("id_shipper") String id_shipper,
            @Field("id_order") String id_order,
            @Field("amount") String amount,
            @Field("transfer_date") String transfer_date,
            @Field("date_added") String date_added);


    // updating order table after transfer for transfer state
    @FormUrlEncoded
    @POST("updateTransferState")
    Call<Result> updateTransferState(
            @Field("id_order") String id_order,
            @Field("id_shipper") String id_shipper,
            @Field("transfer_state") String transfer_state);

    // get total amount received
    @FormUrlEncoded
    @POST("getRecTotalWalletAmountShipper")
    Call<Result> getRecTotalWalletAmountShipper(
            @Field("id_shipper") String id_shipper);

    // get total amount received  based on transfer_date
    @FormUrlEncoded
    @POST("getRecTotalWalletAmountShipperDate")
    Call<Result> getRecTotalWalletAmountShipperDate(
            @Field("id_shipper") String id_shipper,
            @Field("transfer_date") String transfer_date);

    // get total amount received  based on monthly transfer_date
    @FormUrlEncoded
    @POST("getRecTotalWalletAmountShipperMonth")
    Call<Result> getRecTotalWalletAmountShipperMonth(
            @Field("id_shipper") String id_shipper,
            @Field("year") String year,
            @Field("month") String month);
}
