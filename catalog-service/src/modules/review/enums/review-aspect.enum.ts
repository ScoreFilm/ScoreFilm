import { registerEnumType } from '@nestjs/graphql';

export enum ReviewAspect {
  SCRIPT = 'SCRIPT',
  CHARACTERS = 'CHARACTERS',
  PHOTOGRAPHY = 'PHOTOGRAPHY',
  MUSIC = 'MUSIC',
  DIRECTION = 'DIRECTION',
  ACTING = 'ACTING',
  ANIMATION = 'ANIMATION',
  ACTION = 'ACTION',
  COMEDY = 'COMEDY',
  ENDING = 'ENDING',
  ORIGINALITY = 'ORIGINALITY',
}

registerEnumType(ReviewAspect, {
  name: 'ReviewAspect',
});
