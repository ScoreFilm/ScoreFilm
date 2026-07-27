import { UseGuards } from '@nestjs/common';
import { Query, Resolver } from '@nestjs/graphql';
import { GqlAuthGuard } from './common/guards/gql-auth.guard';
import { CurrentUser } from './common/decorators/current-user.decorator';

@Resolver()
export class AppResolver {
  @UseGuards(GqlAuthGuard)
  @Query(() => String)
  whoAmI(@CurrentUser() userId: string) {
    return userId;
  }
}
