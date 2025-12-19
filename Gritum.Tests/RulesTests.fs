module RulesTests

open Xunit
open Gritum.Model
open Gritum.Rules
open Samples

[<Fact>]
let missingTotalClosingCostsYieldsRuleError () =
    let loan: Loan =
        { id = LoanId "fake-loan-id"
          purpose = Purchase }
    let docs: DocumentSnapshot list =
        [ leDocWoTcc; leDocWiTcc; cdDocWoTcc ]
    let input: PrecheckInput =
        { loan = loan ; documentSnapshots = docs }
    let result : RuleCheckResult =
        checkTotalClosingCosts input
    match result with
    | Ok _ ->
        Assert.True(false, "Expected Error, but got Ok")
    | Error (errors: RuleErrors) ->
        Assert.Equal(2, errors.Length)
        Assert.Contains(MissingField LoanEstimate, errors)
        Assert.Contains(MissingField ClosingDisclosure, errors)

[<Fact>]
let successOnTotalClosingCostsYieldsRule () =
    let loan: Loan =
        { id = LoanId "fake-loan-id"
          purpose = Purchase }
    let input: PrecheckInput =
        { loan = loan ; documentSnapshots = [ leDocWiTcc; leDocWiTcc; cdDocWiTcc ] }
    let result : RuleCheckResult =
        checkTotalClosingCosts input
    match result with
    | Ok (Some _) -> Assert.True false
    | Ok None -> Assert.True true
    | Error _ -> Assert.True false