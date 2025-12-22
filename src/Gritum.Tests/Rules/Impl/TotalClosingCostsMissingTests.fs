module Gritum.Rules.Impl.TotalClosingCostsMissing.Tests

open System
open Xunit
open Gritum.Model
open Gritum.Rules.Impl.TotalClosingCostsMissing
open Gritum.TestHelper

let private le (d: DateOnly option) (tcc: Money option) : DocumentSnapshot =
    LE { effectiveDate = d
         totalClosingCosts = tcc }

let private cd (d: DateOnly option) (tcc: Money option) : DocumentSnapshot =
    CD { effectiveDate = d
         totalClosingCosts = tcc }

let private mkInput (snaps: DocumentSnapshot list) : PrecheckInput =
    { loan = { id = LoanId "fake-loan-id"
               purpose = Purchase }
      documentSnapshots = snaps }

[<Fact>]
let ``Representative is latest effectiveDate; older missing TCC is ignored`` () =
    let d1 = DateOnly(2025, 1, 1)
    let d2 = DateOnly(2025, 2, 1)

    let input =
        mkInput
            [ le (Some d1) None
              le (Some d2) (Some (money 123.45m))
              cd (Some d2) (Some (money 10m)) ]

    match check input with
    | Ok findingOpt ->
        Assert.True(findingOpt.IsNone)
    | Error errs ->
        Assert.True(false, $"Expected Ok None, got Error: {errs}")

[<Fact>]
let ``Latest LE missing TCC -> MissingField(LE, TotalClosingCosts)`` () =
    let d1 = DateOnly(2025, 1, 1)
    let d2 = DateOnly(2025, 2, 1)

    let input =
        mkInput
            [ le (Some d1) (Some (money 1m))
              le (Some d2) None
              cd (Some d2) (Some (money 10m)) ]

    match check input with
    | Ok _ ->
        Assert.True(false, "Expected Error, got Ok")
    | Error errs ->
        Assert.Equal<RuleError list>(
            [ MissingField (DocumentType.LoanEstimate, FieldName.TotalClosingCosts) ],
            errs )

[<Fact>]
let ``Missing CD document -> MissingDocument ClosingDisclosure`` () =
    let d = DateOnly(2025, 2, 1)

    let input =
        mkInput
            [ le (Some d) (Some (money 1m)) ]

    match check input with
    | Ok _ ->
        Assert.True(false, "Expected Error, got Ok")
    | Error errs ->
        Assert.Equal<RuleError list>(
            [ MissingDocument DocumentType.ClosingDisclosure ],
            errs )

[<Fact>]
let ``Multiple LE but all effectiveDate None -> InvalidField(LE, EffectiveDate, ParseFailure)`` () =
    let input =
        mkInput
            [ le None (Some (money 1m))
              le None (Some (money 2m))
              cd (Some (DateOnly(2025, 2, 1))) (Some (money 10m)) ]

    match check input with
    | Ok _ ->
        Assert.True(false, "Expected Error, got Ok")
    | Error errs ->
        Assert.Equal<RuleError list>(
            [ InvalidField (DocumentType.LoanEstimate, FieldName.EffectiveDate, InvalidReason.ParseFailure) ],
            errs )

[<Fact>]
let ``Tie on max effectiveDate -> InvalidField(LE, EffectiveDate, OutOfRange)`` () =
    let d = DateOnly(2025, 2, 1)

    let input =
        mkInput
            [ le (Some d) (Some (money 1m))
              le (Some d) (Some (money 2m))
              cd (Some d) (Some (money 10m)) ]

    match check input with
    | Ok _ ->
        Assert.True(false, "Expected Error, got Ok")
    | Error errs ->
        Assert.Equal<RuleError list>(
            [ InvalidField (DocumentType.LoanEstimate, FieldName.EffectiveDate, InvalidReason.OutOfRange) ],
            errs )