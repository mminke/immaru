export default class CollectionRepository {

    static headers: HeadersInit = {'Accept': 'application/json', 'Content-Type':'application/json'}

    async collections() {
        let collections = fetch('/collections', {headers: CollectionRepository.headers})
            .then(response => response.json())

        return collections
    }

    async create(collection: {"name": String}) {
        let result = fetch('/collections', {
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

export type Collection = {
    id: string,
    name: string,
}
