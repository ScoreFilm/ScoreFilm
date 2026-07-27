import { registerEnumType } from '@nestjs/graphql';

export enum TimeWindow {
  DAY = 'day',
  WEEK = 'week',
}

registerEnumType(TimeWindow, { name: 'TimeWindow' });
