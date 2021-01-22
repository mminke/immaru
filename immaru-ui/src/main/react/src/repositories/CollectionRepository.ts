export type Collection = {
    id: string,
    name: string,
}

export default class CollectionRepository {

    static headers: HeadersInit = {'Accept': 'application/json', 'Content-Type':'application/json'}

    async collections(): Promise<Collection[]> {
        let collections = fetch('/collections', {headers: CollectionRepository.headers})
            .then(response => {
                if(!response.ok) {
                    return []
                } else {
                    return response.json()
                }
            })

        return collections
    }

    async create(collection: {"name": String}): Promise<any> {
        fetch('/collections', {
            method: 'POST',
            headers: CollectionRepository.headers,
            body: JSON.stringify(collection)
        })
        .then(response => response.json())
        .catch(error => {
            console.error('Error creating new collection:', error)
        })
    }
}

export const collectionRepository = new CollectionRepository()