import type { Owner, OwnerFields, Pet, PetFields, PetType, Vet, Visit } from './types';

const CUSTOMERS = '/api/customer';
const VETS = '/api/vet';
const VISITS = '/api/visit';
const GATEWAY = '/api/gateway';

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
  });
  if (!response.ok) {
    const problem = await response.json().catch(() => null);
    throw new ApiError(problem?.detail ?? problem?.title ?? response.statusText, response.status);
  }
  return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
}

export const findOwners = (lastName?: string) =>
  request<Owner[]>(`${CUSTOMERS}/owners${lastName ? `?lastName=${encodeURIComponent(lastName)}` : ''}`);

/** Composed by the gateway: owner from customers-service, visits from visits-service. */
export const getOwner = (ownerId: number) => request<Owner>(`${GATEWAY}/owners/${ownerId}`);

export const createOwner = (fields: OwnerFields) =>
  request<Owner>(`${CUSTOMERS}/owners`, { method: 'POST', body: JSON.stringify(fields) });

export const updateOwner = (ownerId: number, fields: OwnerFields) =>
  request<Owner>(`${CUSTOMERS}/owners/${ownerId}`, { method: 'PUT', body: JSON.stringify(fields) });

export const getPetTypes = () => request<PetType[]>(`${CUSTOMERS}/petTypes`);

export const getPet = (petId: number) => request<Pet>(`${CUSTOMERS}/pets/${petId}`);

export const createPet = (ownerId: number, fields: PetFields) =>
  request<Pet>(`${CUSTOMERS}/owners/${ownerId}/pets`, { method: 'POST', body: JSON.stringify(fields) });

export const updatePet = (ownerId: number, petId: number, fields: PetFields) =>
  request<Pet>(`${CUSTOMERS}/owners/${ownerId}/pets/${petId}`, {
    method: 'PUT',
    body: JSON.stringify(fields),
  });

export const createVisit = (petId: number, visit: Omit<Visit, 'id' | 'petId'>) =>
  request<Visit>(`${VISITS}/pets/${petId}/visits`, { method: 'POST', body: JSON.stringify(visit) });

export const getVets = () => request<Vet[]>(`${VETS}/vets`);
