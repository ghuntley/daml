// Copyright (c) 2019 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.digitalasset.platform.sandbox.stores.ledger

import java.time.Instant

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.digitalasset.api.util.TimeProvider
import com.digitalasset.daml.lf.transaction.Node.GlobalKey
import com.digitalasset.daml.lf.value.Value
import com.digitalasset.daml.lf.value.Value.AbsoluteContractId
import com.digitalasset.ledger.backend.api.v1.{SubmissionResult, TransactionSubmission}
import com.digitalasset.platform.sandbox.stores.{ActiveContracts, ActiveContractsInMemory}
import ActiveContracts.ActiveContract
import com.digitalasset.platform.sandbox.stores.ledger.inmemory.InMemoryLedger
import com.digitalasset.platform.sandbox.stores.ledger.sql.{SqlLedger, SqlStartMode}

import scala.collection.immutable
import scala.concurrent.Future

trait Ledger extends AutoCloseable {

  def ledgerId: String

  def ledgerEntries(offset: Option[Long]): Source[(Long, LedgerEntry), NotUsed]

  def ledgerEnd: Long

  def snapshot(): Future[LedgerSnapshot]

  def lookupContract(contractId: Value.AbsoluteContractId): Future[Option[ActiveContract]]

  def lookupKey(key: GlobalKey): Future[Option[AbsoluteContractId]]

  def publishHeartbeat(time: Instant): Future[Unit]

  def publishTransaction(transactionSubmission: TransactionSubmission): Future[SubmissionResult]
}

object Ledger {

  type LedgerFactory = (ActiveContractsInMemory, Seq[LedgerEntry]) => Ledger

  def inMemory(
      ledgerId: String,
      timeProvider: TimeProvider,
      acs: ActiveContractsInMemory,
      ledgerEntries: Seq[LedgerEntry]): Ledger =
    new InMemoryLedger(ledgerId, timeProvider, acs, ledgerEntries)

  def postgres(
      jdbcUrl: String,
      ledgerId: String,
      timeProvider: TimeProvider,
      ledgerEntries: Seq[LedgerEntry],
      startMode: SqlStartMode
  )(implicit mat: Materializer): Future[Ledger] =
    //TODO (robert): casting from Seq to immutable.Seq, make ledgerEntries immutable throughout the Sandbox?
    SqlLedger(jdbcUrl, Some(ledgerId), timeProvider, immutable.Seq(ledgerEntries: _*), startMode)

}
