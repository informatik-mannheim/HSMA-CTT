package de.hs_mannheim.informatik.ct.persistence.services;


import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VisitorService {
    @Autowired
    private VisitorRepository visitorRepo;

    public Optional<Visitor> findVisitorByEmail(String email){
        return visitorRepo.findByEmail(email);
    }

    @Transactional
    public Visitor findOrCreateVisitor(String email) {
        return findVisitorByEmail(email)
                .orElseGet(() -> visitorRepo.save(new Visitor(email)));
    }
}
