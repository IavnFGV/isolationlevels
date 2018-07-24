package investigation;

import investigation.domain.Person;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

public class AppTest {

    private static SessionFactory sessionFactory;
    private Properties properties ;
    private int isolationLevel =1;

    @Before
    public  void beforeClass() throws IOException {
        buildProperties();
        sessionFactory = sessionFactory();
    }

    private void buildProperties() throws IOException {
        properties = new Properties();
        InputStream resourceAsStream = getClass().getResourceAsStream("hibernate.properties");
        properties.load(resourceAsStream);
        resourceAsStream.close();

        properties.setProperty("hibernate.connection.url" , MessageFormat.format("jdbc:p6spy:h2:mem:test;LOCK_MODE={0};mv_store=false",isolationLevel));
    }

    @After
    public  void afterClass() {
        sessionFactory.close();
    }


    @Test
    public void shouldSaveEntity() {
        Person person = new Person();
        person.setEmail("test@email.com");
        Session session = sessionFactory.openSession();
        Session session2 = sessionFactory.openSession();

        Transaction transaction = session.beginTransaction();
        Transaction transaction2 = session2.beginTransaction();

        session.save(person);

//        se


        Person personFromSession = session.get(Person.class, person.getId());
        Person personFromSession2 = session2.get(Person.class, person.getId());
        transaction.rollback();
        personFromSession2.setEmail("newEmail");
        transaction2.commit();
        session.flush();
        session2.flush();
        session.clear();
        session2.clear();
session.close();
session2.close();
        Assert.assertEquals(person.getEmail(), personFromSession.getEmail());

    }

    private SessionFactory sessionFactory() {
        return new Configuration()
                .configure()
                .addProperties(properties)
                .addAnnotatedClass(Person.class)
                .buildSessionFactory();
    }
}
