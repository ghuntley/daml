// Copyright (c) 2019 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.sandbox.stores.ledger.sql.serialisation

import java.nio.ByteBuffer
import java.security.MessageDigest

import com.digitalasset.daml.lf.value.Value
import com.digitalasset.daml.lf.value.Value.{AbsoluteContractId, VersionedValue}

sealed abstract class HashToken extends Product with Serializable
final case class HashTokenText(value: String) extends HashToken
final case class HashTokenBool(value: Boolean) extends HashToken
final case class HashTokenInt(value: Int) extends HashToken
final case class HashTokenLong(value: Long) extends HashToken
final case class HashTokenBigDecimal(value: BigDecimal) extends HashToken
final case class HashTokenCollectionBegin(length: Int) extends HashToken
final case class HashTokenCollectionEnd() extends HashToken

object ValueHasher {

  /**
    * Computes a hash by folding over the given DAML-LF value.
    * The value is traversed in a stable way, producing "hash tokens" for any encountered primitive values.
    *
    * @param value the DAML-LF value to hash
    * @param z initial hash value
    * @param op operation to append a hash token
    * @return the final hash value
    */
  def foldLeft[T](value: Value[AbsoluteContractId], z: T, op: (T, HashToken) => T): T = {
    import com.digitalasset.daml.lf.value.Value._

    value match {
      case ValueContractId(v) => op(z, HashTokenText(v.coid))
      case ValueInt64(v)      => op(z, HashTokenLong(v))
      case ValueDecimal(v)    => op(z, HashTokenBigDecimal(v))
      case ValueText(v)       => op(z, HashTokenText(v))
      case ValueTimestamp(v)  => op(z, HashTokenLong(v.micros))
      case ValueParty(v)      => op(z, HashTokenText(v.underlyingString))
      case ValueBool(v)       => op(z, HashTokenBool(v))
      case ValueDate(v)       => op(z, HashTokenInt(v.days))
      case ValueUnit          => op(z, HashTokenText("()"))

      // Record: [CollectionBegin(), Token(field value)*, CollectionEnd()]
      case ValueRecord(_, fs) =>
        val z1 = op(z, HashTokenCollectionBegin(fs.length))
        val z2 = fs.foldLeft[T](z1)((t, v) => foldLeft(v._2, t, op))
        op(z2, HashTokenCollectionEnd())

      // Optional: [CollectionBegin(), Text("Some" or "None"), Token(value), CollectionEnd()]
      case ValueOptional(Some(v)) =>
        val z1 = op(z, HashTokenCollectionBegin(2))
        val z2 = op(z1, HashTokenText("Some"))
        val z3 = foldLeft(v, z2, op)
        op(z3, HashTokenCollectionEnd())
      case ValueOptional(None) =>
        val z1 = op(z, HashTokenCollectionBegin(1))
        val z2 = op(z1, HashTokenText("None"))
        op(z2, HashTokenCollectionEnd())

      // Variant: [CollectionBegin(), Text(variant), Token(value), CollectionEnd()]
      case ValueVariant(_, variant, v) =>
        val z1 = op(z, HashTokenCollectionBegin(1))
        val z2 = op(z1, HashTokenText(variant))
        val z3 = foldLeft(v, z2, op)
        op(z3, HashTokenCollectionEnd())

      // List: [CollectionBegin(), Token(value)*, CollectionEnd()]
      case ValueList(xs) =>
        val arr = xs.toImmArray
        val z1 = op(z, HashTokenCollectionBegin(xs.length))
        val z2 = arr.foldLeft[T](z1)((t, v) => foldLeft(v, t, op))
        op(z2, HashTokenCollectionEnd())

      // Map: [CollectionBegin(), (Text(key), Token(value))*, CollectionEnd()]
      case ValueMap(xs) =>
        val arr = xs.toImmArray
        val z1 = op(z, HashTokenCollectionBegin(arr.length))
        val z2 = arr.foldLeft[T](z1)((t, v) => {
          val zz1 = op(t, HashTokenText(v._1))
          foldLeft(v._2, zz1, op)
        })
        op(z2, HashTokenCollectionEnd())

      // Tuple: same as Map
      case ValueTuple(xs) =>
        val z1 = op(z, HashTokenCollectionBegin(xs.length))
        val z2 = xs.foldLeft[T](z1)((t, v) => {
          val zz1 = op(t, HashTokenText(v._1))
          foldLeft(v._2, zz1, op)
        })
        op(z2, HashTokenCollectionEnd())
    }
  }

  def hashValue(value: VersionedValue[AbsoluteContractId]): Array[Byte] = {
    val languageVersion = ???
    val valueVersion = value.version

    // TODO: The way bytes are pushed to the digest is probably super inefficient
    // TODO: Prefix variable length primitives with their size (text, bigdecimal)!
    val digest = foldLeft[MessageDigest](value.value, MessageDigest.getInstance("SHA-256"), (d, token) => token match {
      case HashTokenText(v)                 => d.update(v.getBytes("UTF8")); d
      case HashTokenBool(v)                 => d.update(if (v) 1.toByte else 0.toByte); d
      case HashTokenInt(v)                  => d.update(ByteBuffer.allocate(8).putInt(v).array()); d
      case HashTokenLong(v)                 => d.update(ByteBuffer.allocate(8).putLong(v).array()); d
      case HashTokenBigDecimal(v)           => d.update(v.toString.getBytes("UTF8")); d // TODO: is this stable?
      case HashTokenCollectionBegin(length) => d.update(ByteBuffer.allocate(8).putInt(length).array()); d
      case HashTokenCollectionEnd()         => d // no-op
    })

    digest.digest()
  }

}
