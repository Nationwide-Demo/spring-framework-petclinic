export interface Visit {
  id?: number;
  petId?: number;
  date: string;
  description: string;
}

export interface Pet {
  id: number;
  name: string;
  birthDate: string | null;
  typeId: number | null;
  type: string | null;
  ownerId?: number;
  ownerName?: string;
  visits?: Visit[];
}

export interface Owner {
  id: number;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  telephone: string;
  pets: Pet[];
}

export interface PetType {
  id: number;
  name: string;
}

export interface Vet {
  id: number;
  firstName: string;
  lastName: string;
  specialties: string[];
}

export type OwnerFields = Omit<Owner, 'id' | 'pets'>;

export interface PetFields {
  name: string;
  birthDate: string | null;
  typeId: number;
}
