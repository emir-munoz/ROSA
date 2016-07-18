package org.rosa.discovery;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of RDF4J functions.
 *
 * @author Emir Munoz
 * @version 0.0.1
 * @since 01/07/2016
 */
public class RDF4JTest {

    private Repository repository;

    @Before
    public void createRepo() {
        repository = new SailRepository(new MemoryStore());
        repository.initialize();
    }

    @Test
    /**
     * Load test triples into repository.
     *
     * @param repository Sesame repository.
     */
    public void loadRDFTest() {
        ValueFactory factory = repository.getValueFactory();

        IRI bob = factory.createIRI("http://example.org/bob");
        IRI name = factory.createIRI("http://example.org/name");
        Literal bobsName = factory.createLiteral("Bob");
        Statement nameStatement = factory.createStatement(bob, name, bobsName);

        // Loading statements programmatically
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.begin();
            conn.add(nameStatement);
            conn.commit();
        }
    }

    @After
    public void queryRepo() {
        try (RepositoryConnection conn = repository.getConnection()) {
            String queryString = "SELECT * WHERE { ?sub ?pred ?obj . }";
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                BindingSet bindingSet;
                Value sub, pred, obj;
                while (result.hasNext()) {
                    bindingSet = result.next();
                    sub = bindingSet.getValue("sub");
                    pred = bindingSet.getValue("pred");
                    obj = bindingSet.getValue("obj");
                    System.out.println(sub.stringValue() + "\t" + pred.stringValue() +
                            "\t" + obj.stringValue());
                }
            }
        }
    }

}
