package com.corebank.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionManager {

   HikariDataSource dataSource;
   public static DBConnectionManager instance;
   public static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);


   public static DBConnectionManager getInstance() {
       if(instance == null)
       {
            synchronized (DBConnectionManager.class)
            {
                if(instance == null)
                {
                    instance = new DBConnectionManager();
                }

            }
       }
       return instance;
   }

   private DBConnectionManager() {
       init();
   }

  private void init(){
       Properties properties = new Properties();
       try(InputStream input = getClass().getClassLoader().getResourceAsStream("applcation.properties")){
          if(input == null)
          {
              throw new RuntimeException("applcation.properties not found in classpath");
          }
          properties.load(input);
       }
       catch(IOException e)
       {
           throw new RuntimeException(e);
       }

       HikariConfig config = new HikariConfig(properties);
       config.setJdbcUrl(properties.getProperty("db,url"));
       config.setUsername(properties.getProperty("db,username"));
       config.setPassword(properties.getProperty("db.password"));
       config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.maximumPoolSize", "10")));
       config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.minimumIdle", "2")));
       config.setIdleTimeout(Long.parseLong(properties.getProperty("db.idleTimeout", "30000")));
       config.setConnectionTimeout(Long.parseLong(properties.getProperty("db.connectionTimeout", "30000")));
       config.setMaxLifetime(Long.parseLong(properties.getProperty("db.maxLifetime", "1800000")));

       dataSource = new HikariDataSource(config);

       logger.info("HIKARICP CONNECTION POOL ESTSABLISHED SUCCESSFULLY");
  }

  public Connection getConnection() throws SQLException{

       return dataSource.getConnection();
  }

  public void shutDown()
  {
      if(dataSource != null && !dataSource.isClosed())
      {
          dataSource.close();
          logger.info("HikariCP connection pool closed");
      }
  }

}
