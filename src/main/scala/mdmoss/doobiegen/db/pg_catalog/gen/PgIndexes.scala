package mdmoss.doobiegen.db.pg_catalog.gen

/* Todo handle imports better */
import doobie.imports._
import java.sql.{Date, Timestamp, Time}
import java.util.UUID
import java.time.{LocalDate, LocalDateTime}
import scalaz._, Scalaz._

import doobie.postgres.imports._

object PgIndexes extends PgIndexes {

  case class Row(
    indexdef: Option[String]
  ) {
    def toShape: Shape = Shape.NoDefaults(indexdef)
  }

  object Row {
    val ColumnsFragment: Fragment = fr"indexdef"
    def aliasedColumnsFragment(a: String): Fragment = Fragment.const0(a) ++ fr".indexdef"
  }

  case class Shape(indexdef: Option[String] = None)

  object Shape {
    def NoDefaults(indexdef: Option[String]): Shape = Shape(indexdef)
  }

    private val zippedRowComposite = Composite.fromMetaOption(doobie.util.meta.Meta.StringMeta)

    implicit def RowComposite: Composite[Row] = {
      zippedRowComposite.xmap(
        t => Row(t),
        (row) => (row.indexdef)
      )
    }

    private val zippedShapeComposite = Composite.fromMetaOption(doobie.util.meta.Meta.StringMeta)

    implicit def ShapeComposite: Composite[Shape] = {
      zippedShapeComposite.xmap(
        t => Shape(t),
        (row) => (row.indexdef)
      )
    }

}
trait PgIndexes {
  import PgIndexes._

  def create(indexdef: Option[String] = None): ConnectionIO[Row] = {
    create(Shape(indexdef))
  }

  def createVoid(indexdef: Option[String] = None): ConnectionIO[Unit] = {
    createVoid(Shape(indexdef))
  }

  private[gen] def insertMany(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgIndexes.Shape]): Update[Shape] = {
    val sql = "INSERT INTO pg_catalog.pg_indexes (indexdef) VALUES (?)"
    Update[Shape](sql)
  }

  private[gen] def createManyP(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgIndexes.Shape]): scalaz.stream.Process[ConnectionIO, Row] = {
    insertMany(values).updateManyWithGeneratedKeys[Row]("indexdef")(values)
  }

  def createMany(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgIndexes.Shape]): ConnectionIO[List[Row]] = {
    if (values.nonEmpty) createManyP(values).runLog.map(_.toList) else List.empty.point[ConnectionIO]
  }

  def createManyVoid(values: List[mdmoss.doobiegen.db.pg_catalog.gen.PgIndexes.Shape]): ConnectionIO[Unit] = {
    if (values.nonEmpty) insertMany(values).updateMany[List](values).map(_ => ()) else ().point[ConnectionIO]
  }

  def create(shape: mdmoss.doobiegen.db.pg_catalog.gen.PgIndexes.Shape): ConnectionIO[Row] = {
    createMany(shape :: Nil).map(_.head)
  }

  def createVoid(shape: mdmoss.doobiegen.db.pg_catalog.gen.PgIndexes.Shape): ConnectionIO[Unit] = {
    createManyVoid(shape :: Nil)
  }

  private[gen] def allInner(offset: Long, limit: Long): Query0[Row] = {
    (sql"""
      SELECT """ ++ Row.ColumnsFragment ++ sql"""
      FROM pg_catalog.pg_indexes
      OFFSET $offset
      LIMIT $limit
    """).query[Row]
  }

  def all(offset: Long, limit: Long): ConnectionIO[List[Row]] = {
    allInner(offset, limit).list
  }

  def allUnbounded(): ConnectionIO[List[Row]] = {
    allInner(0, 9223372036854775807L).list
  }

  private[gen] def countInner(): Query0[Long] = {
    sql"""
      SELECT COUNT(*)
      FROM pg_catalog.pg_indexes
    """.query[Long]
  }
  def count(): ConnectionIO[Long] = {
    countInner().unique
  }

}