package com.earthrevealed.immaru.api.v1

import com.earthrevealed.immaru.domain.CollectionId
import com.earthrevealed.immaru.domain.Tag
import com.earthrevealed.immaru.domain.TagId
import com.earthrevealed.immaru.domain.tag
import com.earthrevealed.immaru.persistence.CollectionRepository
import com.earthrevealed.immaru.persistence.TagRepository
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@ApiResponse
@RestController
@RequestMapping("/collections/{collectionId}")
class TagResource(
        val collectionRepository: CollectionRepository,
        val tagRepository: TagRepository
) {

    @GetMapping("/tags", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun tags(
            @PathVariable("collectionId") collectionId: CollectionId
    ): List<Tag> {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }

        return tagRepository.all(collectionId)
    }

    @GetMapping("/tags/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun tag(
            @PathVariable("collectionId") collectionId: CollectionId,
            @PathVariable("id") id: TagId
    ): Tag {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }

        return tagRepository.get(collectionId, id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Tag with id ${id.value} does not exist.")
    }


    @PostMapping("/tags", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTag(
            @PathVariable("collectionId") collectionId: CollectionId,
            @RequestBody newTag: NewTag
    ): ResponseEntity<CreationResult> {
        if (collectionRepository.notExists(collectionId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Collection with id ${collectionId.value} does not exist.")
        }

        val tag = tag(collectionId) {
            name = newTag.name
        }
        tagRepository.save(tag)

        val locations = listOf<String>("/collections/${collectionId.value}/tags/${tag.id.value}")
        return ResponseEntity(CreationResult(locations), HttpStatus.CREATED)
    }
}

data class NewTag(var name: String = "Unknown")
