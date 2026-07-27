import { DynamicModule, Module } from '@nestjs/common';
import {
  HttpModule as NestHttpModule,
  type HttpModuleOptions,
} from '@nestjs/axios';
import { ConfigModule } from '@nestjs/config';
import { TmdbClient } from './clients/tmdb.client';
import { TmdbService } from './tmdb.service';
import { TmdbResolver } from './tmdb.resolver';

const HttpModule = NestHttpModule as {
  register(config: HttpModuleOptions): DynamicModule;
};

@Module({
  imports: [
    HttpModule.register({
      timeout: 5000,
      maxRedirects: 3,
    }),
    ConfigModule,
  ],
  providers: [TmdbClient, TmdbService, TmdbResolver],
  exports: [TmdbService],
})
export class TmdbModule {}
