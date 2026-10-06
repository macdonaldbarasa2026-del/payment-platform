#include <iostream>
#include <string>

struct Payment {
    std::string reference;
    long long amount;
    std::string currency;
};

int main() {
    Payment payment{
        "BOOT-TEST",
        1000,
        "KES"
    };

    std::cout << "Payment Core online\n";
    std::cout << "Reference: " << payment.reference << "\n";
    std::cout << "Amount: " << payment.amount << "\n";
    std::cout << "Currency: " << payment.currency << "\n";

    return 0;
}
