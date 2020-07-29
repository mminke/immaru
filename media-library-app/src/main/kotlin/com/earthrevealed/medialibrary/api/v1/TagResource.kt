package com.earthrevealed.medialibrary.api.v1

import com.earthrevealed.medialibrary.domain.TagId
import com.earthrevealed.medialibrary.domain.tag
import com.earthrevealed.medialibrary.persistence.TagRepository
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*

@ApiResponse
@RestController
class TagResource(
        val tagRepository: TagRepository
) {

    @GetMapping("/tags", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun tags() =
            tagRepository.all()

    @GetMapping("/tags/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun tag(@PathVariable("id") id: UUID) =
        tagRepository.get(TagId(id))

    @PostMapping("/tags", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTag( @RequestBody newTag: NewTag) {
        val tag = tag {
            name = newTag.name
        }
        tagRepository.save(tag)
    }
}

data class NewTag(var name: String) {
    constructor(): this("unknown")
}