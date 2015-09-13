package com.chinthaka.imagesimilarity.db

import java.sql.{ResultSet, Statement, PreparedStatement, DriverManager, Connection}
import java.util.logging.Logger

import com.chinthaka.imagesimilarity.util.GlobalContext

/**
 * @author - Eran Withana (eran.chinthaka@gmail.com)
 */

object DBManager {

  val logger: Logger = Logger.getLogger(this.getClass.getName)

  val DBHostName = GlobalContext.config.getString("app.db.hostname")
  val DBPort = GlobalContext.config.getInt("app.db.port")
  val DBName = GlobalContext.config.getString("app.db.schema")
  val DBUsername = GlobalContext.config.getString("app.db.username")
  val DBPassword = GlobalContext.config.getString("app.db.password")

  def getConnection(jdbcURL: String = s"jdbc:postgresql://$DBHostName:$DBPort/$DBName",
                    dbUsername: String = DBUsername,
                    dbPassword: String = DBPassword,
                    timeout: Int = 1, // timeout in seconds
                    maxRetries: Int = 3): Option[Connection] = {

    logger.info(s"DB Parameters $DBUsername@${DBHostName}:${DBPort}/$DBName")
    
    try {

      val c = Iterator
              .continually(DriverManager.getConnection(jdbcURL, dbUsername, dbPassword))
              .zipWithIndex
              .dropWhile({
                           case (conn, i) =>
                             if (i >= maxRetries)
                               throw new IllegalStateException(s"Failed after $i tries, giving up")
                             if (i > 0)
                               logger.info(s"Retry ${i + 1}/$maxRetries to get DB connection for ${jdbcURL} after $timeout seconds")
                             // Ref: http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html#isValid(int)
                             !conn.isValid(timeout)
                         })
              .next
              ._1
      Option(c)
    } catch {
      case e: Exception => {
        val msg = s"Error in creating a connection for ${jdbcURL}"
        println(msg, e)
        throw new IllegalStateException(msg)
      }
    }
  }

  def insertImageMetadata(imageMetadata: ImageMetadata) {
    getConnection() match {
      case Some(connection) => {
        val preparedStatement: PreparedStatement = connection
                                                   .prepareStatement(s"INSERT into ${ImageMetadata.Schema}.${ImageMetadata.Table} (" +
                                                                     s" ${ImageMetadata.UUID}," +
                                                                     s" ${ImageMetadata.LowResHist}," +
                                                                     s" ${ImageMetadata.highResHist}) values (?, ?, ?)")
        preparedStatement.setString(1, imageMetadata.uuid)
        preparedStatement.setString(2, imageMetadata.lowResHist)
        preparedStatement.setString(3, imageMetadata.highResHist)
        preparedStatement.execute()
        preparedStatement.close()
        connection.close()
      }
      case _ => {
        logger.severe("Couldn't get a connection to the database")

      }
    }
  }

  def retrieveImagesWithProperty(propertyName: String, value: String, constraintName: Option[String] = None, constraintValue: Option[String] = None): Option[List[ImageMetadata]] = {
    getConnection() match {
      case Some(connection) => {
        val statement: Statement = connection.createStatement()

        val optionalWhereClause = if (constraintName.nonEmpty) s" and  ${constraintName.get} != \'${constraintValue.get}\'" else ""
        val sql: String = s"SELECT * from ${ImageMetadata.Schema}.${ImageMetadata.Table} WHERE $propertyName = \'$value\' $optionalWhereClause"
        logger.info(s"Executing sql $sql")
        val resultSet: ResultSet = statement.executeQuery(sql)

        val imageMetaData = Iterator.continually(resultSet).takeWhile(_.next).map(rs =>
                                                                                   new ImageMetadata(rs.getString("uuid"),
                                                                                                     rs.getString("low_res_hist"),
                                                                                                     rs.getString("high_res_hist"))
                                                                                ).toList
        Option(imageMetaData)
      }
      case _ => {
        logger.severe("Couldn't get a connection to the database")
        None
      }
    }
  }

}
