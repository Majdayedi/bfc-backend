package bfc.bfc.controller;

import bfc.bfc.entities.ClientLogo;
import bfc.bfc.repository.ClientLogoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientLogoController {

    private final ClientLogoRepository clientLogoRepository;

    public ClientLogoController(ClientLogoRepository clientLogoRepository) {
        this.clientLogoRepository = clientLogoRepository;
    }

    @GetMapping
    public List<ClientLogo> getAllClients() {
        return clientLogoRepository.findAllByOrderByDisplayOrderAsc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientLogo> getClientById(@PathVariable Long id) {
        return clientLogoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ClientLogo createClient(@RequestBody ClientLogo client) {
        if (client.getDisplayOrder() == null) {
            client.setDisplayOrder((int) clientLogoRepository.count() + 1);
        }
        return clientLogoRepository.save(client);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientLogo> updateClient(@PathVariable Long id, @RequestBody ClientLogo details) {
        return clientLogoRepository.findById(id).map(existing -> {
            existing.setName(details.getName());
            existing.setLogoUrl(details.getLogoUrl());
            if (details.getDisplayOrder() != null) {
                existing.setDisplayOrder(details.getDisplayOrder());
            }
            return ResponseEntity.ok(clientLogoRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        if (!clientLogoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        clientLogoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<List<ClientLogo>> reorderClients(@RequestBody List<ClientLogo> reorderedList) {
        for (int i = 0; i < reorderedList.size(); i++) {
            ClientLogo item = reorderedList.get(i);
            int order = i + 1;
            if (item.getId() != null) {
                clientLogoRepository.findById(item.getId()).ifPresent(c -> {
                    c.setDisplayOrder(order);
                    clientLogoRepository.save(c);
                });
            }
        }
        return ResponseEntity.ok(clientLogoRepository.findAllByOrderByDisplayOrderAsc());
    }
}
