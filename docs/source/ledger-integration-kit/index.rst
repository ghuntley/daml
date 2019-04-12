.. _ledger-integration-kit:

DAML Ledger Integration Kit
===========================

The DAML Ledger Integration Kit allows third-party ledger developers to
implement a DAML Ledger on top of their distributed-ledger or database of
choice. How does that work?

.. TODO::

   Expose DAML-LF spec in docs and link to it.

A DAML Ledger implementation is a server serving the
:doc:`/app-dev/ledger-api-introduction/index` as per the semantics defined in
the :doc:`/concepts/ledger-model/index` and the DAML-LF specification.
Theoretically, you could
implement such a server from scratch following the above documentation.
Practically, we recommend though to use our integration kit and follow
the guides below for
:ref:`building <ledger-integration-kit_building>`,
:ref:`testing <ledger-integration-kit_testing>`, and
:ref:`benchmarking <ledger-integration-kit_benchmarking>` your own DAML Ledger
server. This way you can focus on your distributed-ledger or database of
choice and reuse our DAML Ledger server and DAML interpreter code for
implementing the DAML Ledger API.


.. _ledger-integration-kit_building:

Building your own DAML Ledger
-----------------------------

*WIP*

the Ledger Backend API abstracting over the underlying ledger infrastructure
the Ledger API Server library providing the ability to instantiate (at
compile time) a Ledger Server backed by an implementation of the Ledger
Backend API
the DAML Engine library providing the ability to validate DAML transactions,
e.g., before durably committing a transaction to the ledger
testing infrastructure for checking semantic correctness of a ledger-api-server
backed by Ledger Backend API implementation and testing its performance from
the perspective of a DAML Ledger API user.
Each of these components is still evolving or being created. Below we list
the changes that we plan to make to them and the prioritization we currently use.

This integration kit
is going to provide the following five components to developers aiming to
build such a server:

* this documentation with its step-by-step guide
* the ``ledger-participant-state.jar`` artifact, which provides Scala
  interfaces abstracting
* the ``daml-on-x-server.jar``
* the ``

The currently recommended architecture


.. _ledger-integration-kit_testing:

Testing a DAML Ledger
---------------------



.. _ledger-integration-kit_benchmarking:

Benchmarking a DAML Ledger
--------------------------






* What is the DLIK?
* What is its current state?
* How is the DLIK going to evolve?
* What DAML-on-X ledgers are there?
