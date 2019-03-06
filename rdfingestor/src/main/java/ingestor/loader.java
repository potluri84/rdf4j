package ingestor;

import java.io.IOException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.UnsupportedQueryResultFormatException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import com.complexible.stardog.Stardog;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.rdf4j.StardogRepository;
import com.stardog.common.rdf4j.repository.RepositoryConnections;

public class loader {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws TupleQueryResultHandlerException, QueryEvaluationException, UnsupportedQueryResultFormatException, IOException {

		
		
		Person person = new Person();
		
		person.setId(1);
		person.setFirstName("Krishna");
		person.setLastName("Potluri");
		person.setEmailAddress("Krishna.Potluri@gmail.com");
		
		
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace("ex", "http://example.org/")
				.subject("ex:" + person.getId())
					.add(RDF.TYPE, "appmon:Contact")
					.add("appmon:FirstName", person.getFirstName())
					.add("appmon:LastName", person.getLastName())
					.add("appmon:Email", person.getEmailAddress());

		Model model = builder.build();

		Rio.write(model, System.out, RDFFormat.TURTLE);
		
		Stardog aStardog = Stardog.builder().create();

		try {
			
			try (AdminConnection aAdminConnection = AdminConnectionConfiguration.toServer("http://localhost:5820").credentials("admin", "admin").connect()) {
				
				if (aAdminConnection.list().contains("testRDF4J")) {
					aAdminConnection.drop("testRDF4J");
				}

				if(!aAdminConnection.list().contains("testRDF4J"))
				{
					aAdminConnection.newDatabase("testRDF4J").create();
				}


				Repository aRepo = new StardogRepository(ConnectionConfiguration.to("testRDF4J").server("http://localhost:5820").credentials("admin", "admin"));

				aRepo.initialize();

				try {
					try (RepositoryConnection aRepoConn = aRepo.getConnection()) {
						RepositoryConnections.add(aRepoConn, model);

						TupleQuery aQuery = aRepoConn.prepareTupleQuery(QueryLanguage.SPARQL, "select * where { ?s ?p ?o.}");

						try (TupleQueryResult aResults = aQuery.evaluate()) {
							QueryResultIO.writeTuple(aResults, TupleQueryResultFormat.TSV, System.out);
						}
					}
				}
				finally {
					aRepo.shutDown();
				}

				//aAdminConnection.drop("testRDF4J");
			}
		}
		finally {
			aStardog.shutdown();
		}

	}
		
		
	}
