import {User} from "./user.model";
import {Comment} from "./comment.model";

export class DetailedComment {

  comment: Comment;
  user: User;

  deserialize(input: any): this {
    Object.assign(this, input);
    this.comment = new Comment().deserialize(input.comment)
    this.user = new User().deserialize(input.user)
    return this;
  }
}
