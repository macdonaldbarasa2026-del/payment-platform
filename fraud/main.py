from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="Payment Fraud Engine")


class Transaction(BaseModel):
    amount: float
    currency: str = "KES"
    country: str = "KE"


@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "fraud-engine"
    }


@app.post("/score")
def score(transaction: Transaction):
    score = 0.05

    if transaction.amount >= 100000:
        score += 0.25

    if transaction.country != "KE":
        score += 0.10

    score = min(score, 1.0)

    if score >= 0.70:
        decision = "block"
    elif score >= 0.40:
        decision = "review"
    else:
        decision = "allow"

    return {
        "risk_score": score,
        "decision": decision
    }
