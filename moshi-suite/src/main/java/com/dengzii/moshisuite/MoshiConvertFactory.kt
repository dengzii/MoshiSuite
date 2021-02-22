package com.dengzii.moshisuite

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

class MoshiConvertFactory private constructor(private val moshi: Moshi) : Converter.Factory() {

    companion object {
        private val CONTENT_TYPE_JSON = "application/json".toMediaType()

        fun create(
            moshi: Moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        ): MoshiConvertFactory {
            return MoshiConvertFactory(moshi)
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        return MoshiResponseBodyConverter<Any>(type, moshi)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        return MoshiRequestBodyConverter<Any>(type, moshi)
    }

    private class MoshiResponseBodyConverter<T>(
        private val type: Type,
        private val moshi: Moshi
    ) : Converter<ResponseBody, T?> {

        @Throws(IOException::class)
        override fun convert(value: ResponseBody): T? {
            if (CONTENT_TYPE_JSON == value.contentType()) {
                val s = value.source()
                val adapter = moshi.adapter<T>(type)
                return adapter.fromJson(s)
            }
            return null
        }
    }

    private class MoshiRequestBodyConverter<T>(
        private val type: Type,
        private val moshi: Moshi
    ) : Converter<T?, RequestBody> {

        override fun convert(value: T?): RequestBody? {
            value ?: return null
            val adapter = moshi.adapter<T>(type)
            val json = adapter.toJson(value)
            return JsonRequestBody.create(json)
        }
    }
}