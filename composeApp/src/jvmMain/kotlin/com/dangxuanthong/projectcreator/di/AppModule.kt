package com.dangxuanthong.projectcreator.di

import com.dangxuanthong.projectcreator.repository.ProjectConfigRepository
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.CIOEngineConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import java.nio.file.Path
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

@Module
@ComponentScan("com.dangxuanthong.projectcreator")
class AppModule : KoinComponent {

    @Single
    fun provideDotEnv() = dotenv { directory = ".." }

    @Single
    fun provideHttpClientEngine(): HttpClientEngineFactory<CIOEngineConfig> = CIO

    @Single
    @Named("GitHub")
    fun provideHttpClient(
        engine: HttpClientEngineFactory<HttpClientEngineConfig>,
        env: Dotenv
    ) = HttpClient(engine) {
        install(Logging)
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }
        install(Auth) {
            bearer {
                loadTokens { BearerTokens(env["GH_PAT"], null) }
            }
        }
    }

    @Single
    fun provideProjectConfigRepository(): (Path) -> ProjectConfigRepository =
        { get { parametersOf(it) } }
}
