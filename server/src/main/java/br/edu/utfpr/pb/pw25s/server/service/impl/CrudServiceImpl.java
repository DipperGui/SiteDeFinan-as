package br.edu.utfpr.pb.pw25s.server.service.impl;

import br.edu.utfpr.pb.pw25s.server.service.CrudService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public abstract class CrudServiceImpl <T, ID extends Serializable>
        implements CrudService<T, ID> {

    protected abstract JpaRepository<T, ID> getRepository();

    @Override
    public List<T> findAll(){
        return getRepository().findAll();
    }
}
