import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { MongooseModule } from '@nestjs/mongoose';
import { GraphQLModule } from '@nestjs/graphql';
import { ApolloDriver, ApolloDriverConfig } from '@nestjs/apollo';
import configuration from './config/configuration';
import { FavoriteModule } from './modules/favorite/favorite.module';
import { ReviewModule } from './modules/review/review.module';
import { TmdbModule } from './modules/tmdb/tmdb.module';
import { WatchlistModule } from './modules/watchlist/watchlist.module';
import { AuthModule } from './modules/auth/auth.module';
import { AppResolver } from './app.resolver';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [configuration],
    }),

    MongooseModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        uri: config.get<string>('mongodb.uri'),
      }),
    }),

    GraphQLModule.forRoot<ApolloDriverConfig>({
      driver: ApolloDriver,
      autoSchemaFile: true,
      playground: true,
      context: ({ req }) => ({ req }),
    }),

    FavoriteModule,
    AuthModule,
    ReviewModule,
    TmdbModule,
    WatchlistModule,
  ],
  providers: [AppResolver],
})
export class AppModule {}
