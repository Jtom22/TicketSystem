package com.jorge.ticketsystem.backend.ticketSystemBack.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventCreateDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventResponseDto;
import com.jorge.ticketsystem.backend.ticketSystemBack.dto.event.EventUpdateDto;

import com.jorge.ticketsystem.backend.ticketSystemBack.services.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class EventController {
    

    // Esto mejor que el @Autowired
    private final EventService eventService;
 
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        List<EventResponseDto> eventos= eventService.findAll();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        //Como no se puede pasar de Optional a string utilizamos un .map para que nos devuelva un ResponseEntity, comprobando ademas si existe o no evento
        return ResponseEntity.ok(eventService.findById(id));
    }
    
    @PostMapping()
    public ResponseEntity<EventResponseDto>create(@Valid @RequestBody EventCreateDto event ){
       
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(event));
    }

@PutMapping("/{id}")
public ResponseEntity<EventResponseDto> update(@PathVariable Long id,@Valid @RequestBody EventUpdateDto dto) {
      // El 'update' de tu Service devuelve un Optional<EventUpdateDto>
    return ResponseEntity.ok(eventService.update(id, dto));
 
            
}



    @DeleteMapping("/{id}")
    public ResponseEntity<?>delete(@PathVariable Long id){

        //podemos ponerlo asi o con el ok tb
        // eventService.delete(id).map(event->ResponseEntity.ok().body(event)).orElse(ResponseEntity.notFound().build());
        eventService.delete(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content si se borra con éxito
        //de esta manera nos devuelve un 204 no content que se supone es mejor
    }
    

}
