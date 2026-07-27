import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { WatchlistEntry, WatchlistSchema } from './schemas/watchlist.schema';
import { WatchlistService } from './watchlist.service';
import { WatchlistResolver } from './watchlist.resolver';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: WatchlistEntry.name, schema: WatchlistSchema },
    ]),
  ],
  providers: [WatchlistService, WatchlistResolver],
  exports: [WatchlistService],
})
export class WatchlistModule {}
