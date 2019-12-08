import {Post} from "./post.model";
import {User} from "./user.model";
import {DetailedComment} from "./DetailedComment.model";
import {Deserializable} from "./deserializable.model";

export class DetailedPost implements Deserializable {

  post: Post;
  user: User;
  comments: Array<DetailedComment>;
  isExpanded: boolean = false;

  deserialize(input: any): this {
    Object.assign(this, input);
    this.post = new Post().deserialize(input.post)
    this.user = new User().deserialize(input.user)
    this.comments = input.comments.map(comment => new DetailedComment().deserialize(comment));
    return this;
  }

}
