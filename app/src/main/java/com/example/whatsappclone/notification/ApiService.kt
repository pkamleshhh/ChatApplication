//package com.example.whatsappclone.notification
//
//import retrofit2.http.Body
//import retrofit2.http.Header
//import retrofit2.http.POST
//
//interface ApiService {
//    @Header(
//        {
//            "Content-Type:application/json",
//            "Authorization:key=AAAACvd3k3Q:APA91bEPo0ZANp44nkWU1k2EWvZtxbqzLIGS0ZRiPcTknSbdKQn8zKW3Y1dIH3DXAjg1zMlOkob1W3lN46hCskext_Ch8PKizJLwyAOqCkih3RmX0EEWC89MHn-GXzd4z1O0IPcPd89X"
//        }
//    )
//    @POST("fcm/send")
//    Call<MyResponse>sendNotification(@Body Sender body)
//}