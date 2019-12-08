import {Deserializable} from "./deserializable.model";

export class Post implements Deserializable {

  id: string;
  instant: string;
  description: string;
  reportsNumber: number;
  organizationId: string;
  userId: string;
  public: boolean;

  deserialize(input: any): this {
    return Object.assign(this, input);
  }
}
