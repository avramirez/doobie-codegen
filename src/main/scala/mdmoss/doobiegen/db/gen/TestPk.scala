package mdmoss.doobiegen.db.gen

/* Todo handle imports better */
import doobie.imports._
import java.sql.{Date, Timestamp, Time}
import java.util.UUID
import java.time.{LocalDate, LocalDateTime}
import scalaz._, Scalaz._

import doobie.postgres.imports._

object TestPk extends TestPk {

  case class Id(value: Long)

  case class Row(
    id: mdmoss.doobiegen.db.gen.TestPk.Id
  ) {
    def toShape: Shape = Shape.NoDefaults(id.value)
  }

  object Row {
    val ColumnsFragment: Fragment = fr"id"
    def aliasedColumnsFragment(a: String): Fragment = Fragment.const0(a) ++ fr".id"
  }

  case class Shape(id: Long)

  object Shape {
    def NoDefaults(id: Long): Shape = Shape(id)
  }

    implicit def TestPkIdComposite: Composite[Id] = {
      Composite.fromMeta(doobie.util.meta.Meta.LongMeta).xmap(
        (f1) => Id(f1),
        (a) => a.value
      )
    }

    private val zippedRowComposite = implicitly[Composite[mdmoss.doobiegen.db.gen.TestPk.Id]]

    implicit def RowComposite: Composite[Row] = {
      zippedRowComposite.xmap(
        t => Row(t),
        (row) => (row.id)
      )
    }

    private val zippedShapeComposite = Composite.fromMeta(doobie.util.meta.Meta.LongMeta)

    implicit def ShapeComposite: Composite[Shape] = {
      zippedShapeComposite.xmap(
        t => Shape(t),
        (row) => (row.id)
      )
    }

}
trait TestPk {
  import TestPk._

  def create(id: Long): ConnectionIO[Row] = {
    create(Shape(id))
  }

  def createVoid(id: Long): ConnectionIO[Unit] = {
    createVoid(Shape(id))
  }

  private[gen] def insertMany(values: List[mdmoss.doobiegen.db.gen.TestPk.Shape]): Update[Shape] = {
    val sql = "INSERT INTO test_pk (id) VALUES (?)"
    Update[Shape](sql)
  }

  private[gen] def createManyP(values: List[mdmoss.doobiegen.db.gen.TestPk.Shape]): scalaz.stream.Process[ConnectionIO, Row] = {
    insertMany(values).updateManyWithGeneratedKeys[Row]("id")(values)
  }

  def createMany(values: List[mdmoss.doobiegen.db.gen.TestPk.Shape]): ConnectionIO[List[Row]] = {
    if (values.nonEmpty) createManyP(values).runLog.map(_.toList) else List.empty.point[ConnectionIO]
  }

  def createManyVoid(values: List[mdmoss.doobiegen.db.gen.TestPk.Shape]): ConnectionIO[Unit] = {
    if (values.nonEmpty) insertMany(values).updateMany[List](values).map(_ => ()) else ().point[ConnectionIO]
  }

  def create(shape: mdmoss.doobiegen.db.gen.TestPk.Shape): ConnectionIO[Row] = {
    createMany(shape :: Nil).map(_.head)
  }

  def createVoid(shape: mdmoss.doobiegen.db.gen.TestPk.Shape): ConnectionIO[Unit] = {
    createManyVoid(shape :: Nil)
  }

  private[gen] def getInner(id: mdmoss.doobiegen.db.gen.TestPk.Id): Query0[Row] = {
    (sql"""
      SELECT """ ++ Row.ColumnsFragment ++ sql"""
      FROM test_pk
      WHERE test_pk.id = ${id}
    """).query[Row]
  }
  def get(id: mdmoss.doobiegen.db.gen.TestPk.Id): ConnectionIO[Row] = {
    getInner(id).unique
  }

  private[gen] def findInner(id: Long): Query0[Row] = {
    (sql"""
      SELECT """ ++ Row.ColumnsFragment ++ sql"""
      FROM test_pk
      WHERE test_pk.id = ${id}
    """).query[Row]
  }
  def find(id: Long): ConnectionIO[Option[Row]] = {
    findInner(id).option
  }

  private[gen] def allInner(offset: Long, limit: Long): Query0[Row] = {
    (sql"""
      SELECT """ ++ Row.ColumnsFragment ++ sql"""
      FROM test_pk
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
      FROM test_pk
    """.query[Long]
  }
  def count(): ConnectionIO[Long] = {
    countInner().unique
  }

  private[gen] def multigetInnerBase(id: Option[Seq[mdmoss.doobiegen.db.gen.TestPk.Id]]): Query0[Row] = {
    (sql"""
      SELECT """ ++ Row.ColumnsFragment ++ sql"""
      FROM test_pk
      WHERE (${id.isEmpty} OR test_pk.id = ANY(${{id}.toSeq.flatten.map(_.value).toArray}))
    """).query[Row]
  }

  def multiget(id: Seq[mdmoss.doobiegen.db.gen.TestPk.Id]): ConnectionIO[List[Row]] = {
    if (id.nonEmpty) {
      val distinctValues = id.distinct
      for {
        resultRaw    <- multigetInnerBase(Some(distinctValues)).list
        resultGrouped = resultRaw.groupBy(_.id)
      } yield id.toList.flatMap(x => resultGrouped.getOrElse(x, List.empty))
    } else List.empty.point[ConnectionIO]
  }

  private[gen] def updateInner(row: mdmoss.doobiegen.db.gen.TestPk.Row): Update0 = {
    sql"""
      UPDATE test_pk
      SET id = ${row.id}
      WHERE id = ${row.id}
    """.update
  }
  def update(row: mdmoss.doobiegen.db.gen.TestPk.Row): ConnectionIO[Int] = {
    updateInner(row).run
  }

}