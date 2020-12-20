package de.hs_mannheim.informatik.ct.persistence.services;


import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.persistence.repositories.BesucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VisitorService {
    @Autowired
    private BesucherRepository visitorRepo;


    public Optional<Besucher> findVisitorByEmail(String email){
        return visitorRepo.findById(email);
    }

    @Transactional
    public Besucher findOrCreateVisitor(String email) {
        return findVisitorByEmail(email)
                .orElse(
                        visitorRepo.save(new Besucher(email)));
    }
}