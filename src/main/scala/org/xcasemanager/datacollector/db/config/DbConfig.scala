package org.xcasemanager.datacollector.db.config

import ch.rasc.bsoncodec.math.BigDecimalStringCodec
import ch.rasc.bsoncodec.time.LocalDateTimeDateCodec
import com.mongodb.MongoCredential.createCredential
import com.mongodb.{MongoCredential, ServerAddress}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClientSettings

import scala.collection.JavaConverters.seqAsJavaListConverter

object DbConfig {

  import org.bson.codecs.configuration.CodecRegistries
  import org.bson.codecs.configuration.CodecRegistries._
  import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
  import org.mongodb.scala.bson.codecs.Macros._
  import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
  import org.xcasemanager.datacollector.db.data.Project

  private val javaCodecs = CodecRegistries.fromCodecs(
    new LocalDateTimeDateCodec(),
    new BigDecimalStringCodec())

  private val registry: CodecRegistry = CodecRegistries.fromProviders(classOf[Project])

  val settings: MongoClientSettings = MongoClientSettings.builder()
    .applyToClusterSettings(b => b.hosts(List(new ServerAddress("mongodb")).asJava))
    .codecRegistry(fromRegistries(registry, javaCodecs, DEFAULT_CODEC_REGISTRY))
    .build()

  val client: MongoClient = MongoClient(settings)
  val database: MongoDatabase = client.getDatabase("TCM")
  val projects: MongoCollection[Project] = database.getCollection("Projects")
}