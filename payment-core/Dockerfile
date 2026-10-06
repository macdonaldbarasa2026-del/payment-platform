FROM gcc:14

WORKDIR /app

COPY main.cpp .

RUN g++ -std=c++20 -O2 -Wall -Wextra main.cpp -o payment-core

CMD ["./payment-core"]
