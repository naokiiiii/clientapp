package com.t4j.mobilenurse;

import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;
import rx.Observable;

public interface RetroFitApi {
    @Multipart
    @POST("/diagnose")
    public Observable<DiagnoseResponse> updateMultipart(
            @Part("filearg") TypedFile file,                //単一ファイル
            @Part("msg") String text
    );
}

