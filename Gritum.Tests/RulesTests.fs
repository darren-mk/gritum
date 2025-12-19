module RulesTests

open Xunit
open Gritum.Model
open Gritum.Rules
open Helper

[<Fact>]
let missingTotalClosingCostsYieldsRuleError () =
    let loan: Loan =
        { id = LoanId "fake-loan-id"
          purpose = Purchase }
    let docs: DocumentSnapshot list =
        [ { documentType = LoanEstimate
            totalClosingCosts = None }
          { documentType = LoanEstimate
            totalClosingCosts = Some (money 123.44m) }
          { documentType = ClosingDisclosure
            totalClosingCosts = None } ]
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
    let docs: DocumentSnapshot list =
        [ { documentType = LoanEstimate
            totalClosingCosts = Some (money 32.41m) }
          { documentType = LoanEstimate
            totalClosingCosts = Some (money 153.47m) }
          { documentType = ClosingDisclosure
            totalClosingCosts = Some (money 63.12m) } ]
    let input: PrecheckInput =
        { loan = loan ; documentSnapshots = docs }
    let result : RuleCheckResult =
        checkTotalClosingCosts input
    match result with
    | Ok (Some _) -> Assert.True false
    | Ok None -> Assert.True true
    | Error _ -> Assert.True false