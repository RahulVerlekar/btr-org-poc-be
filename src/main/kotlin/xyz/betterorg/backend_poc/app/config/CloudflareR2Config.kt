package xyz.betterorg.backend_poc.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI


@Configuration
class CloudflareR2Config(
    @Value("\${cloudflare.r2.accessKey}") private val accessKey: String,
    @Value("\${cloudflare.r2.secretKey}") private val secretKey: String,
    @Value("\${cloudflare.r2.bucket}") private val bucket: String,
    @Value("\${cloudflare.r2.endpoint}") private val endpoint: String,
) {

    @Bean
    fun s3Client(): S3Client {
        val serviceConfig: S3Configuration? = S3Configuration.builder() // path-style is required for R2
            .pathStyleAccessEnabled(true) // disable AWS4 chunked uploads
            .chunkedEncodingEnabled(false)
            .build()

        return S3Client.builder()
            .httpClientBuilder(ApacheHttpClient.builder())
            .region(Region.of("auto"))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                    )
                )
            )
            .serviceConfiguration(serviceConfig)
            .build()
    }

}