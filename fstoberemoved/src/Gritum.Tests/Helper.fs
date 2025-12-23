module Gritum.TestHelper

open Gritum.Model

let money (x: decimal) : Money =
    Money.create x

let purchaseLoan =
    { id = LoanId "fake-loan-id"
      purpose = Purchase }

let mkInput (snaps: DocumentSnapshot list) : PrecheckInput =
    { loan = purchaseLoan
      documentSnapshots = snaps }