import { Field, Int, ObjectType } from '@nestjs/graphql';

@ObjectType()
export class Genre {
  @Field(() => Int)
  id!: number;

  @Field()
  name!: string;
}
