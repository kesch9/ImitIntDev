package ru.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDAOImpl implements UserDAO {


    public static final Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

    public static SessionFactory sessionFactory;
    public static ServiceRegistry serviceRegistry;
    public Session session;

    public static void init() {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");
        serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    public static void destroy() {
        StandardServiceRegistryBuilder.destroy(serviceRegistry);
    }

    @Override
    public void create(Object user) {
        sessionFactory.getCurrentSession().save(user);
        log.debug("Transaction create");
    }

    @Override
    public void update(Object user) {
        sessionFactory.getCurrentSession().saveOrUpdate(user);
        log.debug("Transaction update");
    }

    @Override
    public Object getById(Long userId) {
        log.debug("Transaction getbyID");
        return (Object) sessionFactory.getCurrentSession().get(Object.class, userId);
    }

    @Override
    public List<Object> getAll(String s) {
        log.debug("Transaction get ListGetALL");
        return sessionFactory.getCurrentSession().createQuery("FROM " + s + " order by login").list();
    }

    @Override
    public void delete(Object user) {
        sessionFactory.getCurrentSession().delete(user);
        log.debug("Transaction delete");
    }
}
