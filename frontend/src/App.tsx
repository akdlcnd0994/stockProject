import React, { useState, useEffect } from 'react';
import './App.css';
import StockChart from './StockChart';

// 타입 정의 - 백엔드 StockDto와 동일
interface StockData {
  stockCode?: string;
  stockName?: string;
  currentPrice?: string;
  previousDayDiff?: string;
  changeRate?: string;
  volume?: string;
  marketCap?: string;
  tradingValue?: string;
  indexValue?: string;
  indexChange?: string;
  topGainers?: StockData[];
  topLosers?: StockData[];
  topMarketCap?: StockData[];
  topVolume?: StockData[];
}

function App() {
  // 상태 관리 - useState 사용
  const [indices, setIndices] = useState<StockData[]>([]);
  const [topGainers, setTopGainers] = useState<StockData | null>(null);
  const [topLosers, setTopLosers] = useState<StockData | null>(null);
  const [topVolume, setTopVolume] = useState<StockData | null>(null);
  const [topMarketCap, setTopMarketCap] = useState<StockData | null>(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<string>('gainers');
  const [selectedStock, setSelectedStock] = useState<{ code: string; name: string } | null>(null);

  // 업데이트된 항목 추적 (애니메이션용)
  const [updatedIndices, setUpdatedIndices] = useState<Set<number>>(new Set());
  const [updatedGainers, setUpdatedGainers] = useState<Set<number>>(new Set());
  const [updatedLosers, setUpdatedLosers] = useState<Set<number>>(new Set());
  const [updatedVolume, setUpdatedVolume] = useState<Set<number>>(new Set());
  const [updatedMarketCap, setUpdatedMarketCap] = useState<Set<number>>(new Set());

  // API 호출 함수들
  const fetchAllIndices = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/stock/indices');
      const newData = await response.json();

      // 변경된 항목 감지
      const updated = new Set<number>();
      newData.forEach((newIndex: StockData, idx: number) => {
        const oldIndex = indices[idx];
        if (oldIndex && (
          oldIndex.indexValue !== newIndex.indexValue ||
          oldIndex.indexChange !== newIndex.indexChange ||
          oldIndex.changeRate !== newIndex.changeRate
        )) {
          updated.add(idx);
        }
      });

      setIndices(newData);
      setUpdatedIndices(updated);

      // 3초 후 애니메이션 클래스 제거
      setTimeout(() => setUpdatedIndices(new Set()), 3000);
    } catch (error) {
      console.error('지수 조회 실패:', error);
    }
  };

  const fetchTopGainers = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/stock/top-gainers');
      const newData = await response.json();

      // 변경된 항목 감지
      const updated = new Set<number>();
      newData?.topGainers?.forEach((newStock: StockData, idx: number) => {
        const oldStock = topGainers?.topGainers?.[idx];
        if (oldStock && (
          oldStock.currentPrice !== newStock.currentPrice ||
          oldStock.changeRate !== newStock.changeRate
        )) {
          updated.add(idx);
        }
      });

      setTopGainers(newData);
      setUpdatedGainers(updated);
      setTimeout(() => setUpdatedGainers(new Set()), 3000);
    } catch (error) {
      console.error('상승률 TOP3 조회 실패:', error);
    }
  };

  const fetchTopLosers = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/stock/top-losers');
      const newData = await response.json();

      const updated = new Set<number>();
      newData?.topLosers?.forEach((newStock: StockData, idx: number) => {
        const oldStock = topLosers?.topLosers?.[idx];
        if (oldStock && (
          oldStock.currentPrice !== newStock.currentPrice ||
          oldStock.changeRate !== newStock.changeRate
        )) {
          updated.add(idx);
        }
      });

      setTopLosers(newData);
      setUpdatedLosers(updated);
      setTimeout(() => setUpdatedLosers(new Set()), 3000);
    } catch (error) {
      console.error('하락률 TOP3 조회 실패:', error);
    }
  };

  const fetchTopVolume = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/stock/top-volume');
      const newData = await response.json();

      const updated = new Set<number>();
      newData?.topVolume?.forEach((newStock: StockData, idx: number) => {
        const oldStock = topVolume?.topVolume?.[idx];
        if (oldStock && (
          oldStock.currentPrice !== newStock.currentPrice ||
          oldStock.tradingValue !== newStock.tradingValue
        )) {
          updated.add(idx);
        }
      });

      setTopVolume(newData);
      setUpdatedVolume(updated);
      setTimeout(() => setUpdatedVolume(new Set()), 3000);
    } catch (error) {
      console.error('거래량 상위 종목 조회 실패:', error);
    }
  };

  const fetchTopMarketCap = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/stock/top-market-cap');
      const newData = await response.json();

      const updated = new Set<number>();
      newData?.topMarketCap?.forEach((newStock: StockData, idx: number) => {
        const oldStock = topMarketCap?.topMarketCap?.[idx];
        if (oldStock && (
          oldStock.currentPrice !== newStock.currentPrice ||
          oldStock.marketCap !== newStock.marketCap
        )) {
          updated.add(idx);
        }
      });

      setTopMarketCap(newData);
      setUpdatedMarketCap(updated);
      setTimeout(() => setUpdatedMarketCap(new Set()), 3000);
    } catch (error) {
      console.error('시가총액 상위 종목 조회 실패:', error);
    }
  };

  // 컴포넌트가 처음 렌더링될 때 실행 - useEffect 사용
  useEffect(() => {
    const fetchAllData = async () => {
      setLoading(true);

      // 순차 호출 (1.5초 간격)
      await fetchAllIndices();
      await new Promise(resolve => setTimeout(resolve, 1500));

      await fetchTopGainers();
      await new Promise(resolve => setTimeout(resolve, 1500));

      await fetchTopLosers();
      await new Promise(resolve => setTimeout(resolve, 1500));

      await fetchTopVolume();
      await new Promise(resolve => setTimeout(resolve, 1500));

      await fetchTopMarketCap();

      setLoading(false);
      console.log("초기 데이터 로드 완료");
    };

    fetchAllData();

    // 30초마다 자동 갱신
    const interval = setInterval(async () => {
      console.log("30초 자동 갱신 시작");
      await fetchAllIndices();
      await new Promise(resolve => setTimeout(resolve, 1500));
      await fetchTopGainers();
      await new Promise(resolve => setTimeout(resolve, 1500));
      await fetchTopLosers();
      await new Promise(resolve => setTimeout(resolve, 1500));
      await fetchTopVolume();
      await new Promise(resolve => setTimeout(resolve, 1500));
      await fetchTopMarketCap();
    }, 30000); // 30초

    // cleanup: 컴포넌트 unmount 시 interval 제거
    return () => clearInterval(interval);
  }, []); // 빈 배열 = 처음 한번만 실행

  // 등락값에 +/- 부호를 추가하는 함수
  const formatPriceChange = (priceChange: string | undefined) => {
    if (!priceChange) return '';
    // 이미 + 또는 - 기호가 있는지 확인
    if (priceChange.startsWith('+') || priceChange.startsWith('-')) {
      return `${priceChange}원`;
    }
    const numValue = Number(priceChange);
    if (numValue > 0) {
      return `+${priceChange}원`; // 양수일 때 + 기호 추가
    } else {
      return `${priceChange}원`; // 음수 또는 0일 경우 그대로
    }
  };

  // 한국어 원화 단위 포맷팅 함수
  const formatKoreanWon = (amount: string | undefined) => {
    if (!amount) return '';
    const num = parseInt(amount);
    if (num >= 1000000000000) {
      return (num / 1000000000000).toFixed(2).replace('.00', '') + '조';
    } else if (num >= 100000000) {
      return (num / 100000000).toFixed(2).replace('.00', '') + '억';
    } else if (num >= 10000) {
      return (num / 10000).toFixed(2).replace('.00', '') + '만';
    }
    return num.toLocaleString();
  };

  // 종목 클릭 핸들러
  const handleStockClick = (stockCode: string | undefined, stockName: string | undefined) => {
    if (stockCode && stockName) {
      setSelectedStock({ code: stockCode, name: stockName });
    }
  };

  // 차트 닫기 핸들러
  const handleCloseChart = () => {
    setSelectedStock(null);
  };

  // 등락률에 +/- 부호를 추가하는 함수
  const formatChangeRate = (changeRate: string | undefined) => {
    if (!changeRate) return '';
    const numValue = Number(changeRate);
    if (numValue > 0) {
      // 이미 + 기호가 있는지 확인
      if (changeRate.startsWith('+')) {
        return `${changeRate}%`;
      } else {
        return `+${changeRate}%`;
      }
    } else {
      return `${changeRate}%`;
    }
  };

  // 로딩 중일 때
  if (loading) {
    return <div className="loading">데이터를 불러오는 중...</div>;
  }

  const renderTabContent = () => {
    switch (activeTab) {
      case 'gainers':
        return (
          <section className="tab-content">
            <h2>상승률 TOP 3</h2>
            {topGainers?.topGainers && (
              <div className="stock-list">
                {topGainers.topGainers.slice(0, 3).map((stock, index) => (
                  <div
                    key={index}
                    className={`stock-item positive ${updatedGainers.has(index) ? 'updated' : ''}`}
                    onClick={() => handleStockClick(stock.stockCode, stock.stockName)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="stock-name">{stock.stockName}</div>
                    <div className="stock-price">
                      {stock.currentPrice}원 ({formatPriceChange(stock.previousDayDiff)}) <span className={`change-rate ${Number(stock.changeRate) >= 0 ? 'positive' : 'negative'}`}>{formatChangeRate(stock.changeRate)}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        );
      case 'losers':
        return (
          <section className="tab-content">
            <h2>하락률 TOP 3</h2>
            {topLosers?.topLosers && (
              <div className="stock-list">
                {topLosers.topLosers.slice(0, 3).map((stock, index) => (
                  <div
                    key={index}
                    className={`stock-item negative ${updatedLosers.has(index) ? 'updated' : ''}`}
                    onClick={() => handleStockClick(stock.stockCode, stock.stockName)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="stock-name">{stock.stockName}</div>
                    <div className="stock-price">
                      {stock.currentPrice}원 ({formatPriceChange(stock.previousDayDiff)}) <span className={`change-rate ${Number(stock.changeRate) >= 0 ? 'positive' : 'negative'}`}>{formatChangeRate(stock.changeRate)}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        );
      case 'volume':
        return (
          <section className="tab-content">
            <h2>거래량 TOP 3</h2>
            {topVolume?.topVolume && (
              <div className="stock-list">
                {topVolume.topVolume.slice(0,3).map((stock, index) => (
                  <div
                    key={index}
                    className={`stock-item volume ${updatedVolume.has(index) ? 'updated' : ''}`}
                    onClick={() => handleStockClick(stock.stockCode, stock.stockName)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="stock-name">{stock.stockName}</div>
                    <div className="stock-price">
                      {stock.currentPrice}원 ({formatPriceChange(stock.previousDayDiff)}) <span className={`change-rate ${Number(stock.changeRate) >= 0 ? 'positive' : 'negative'}`}>{formatChangeRate(stock.changeRate)}</span>
                    </div>
                    <div className="stock-volume">거래대금: {formatKoreanWon(stock.tradingValue)}</div>
                  </div>
                ))}
              </div>
            )}
          </section>
        );
      case 'marketcap':
        return (
          <section className="tab-content">
            <h2>시가총액 TOP 3</h2>
            {topMarketCap?.topMarketCap && (
              <div className="stock-list">
                {topMarketCap.topMarketCap.slice(0,3).map((stock, index) => (
                  <div
                    key={index}
                    className={`stock-item market-cap ${updatedMarketCap.has(index) ? 'updated' : ''}`}
                    onClick={() => handleStockClick(stock.stockCode, stock.stockName)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="stock-name">{stock.stockName}</div>
                    <div className="stock-price">
                      {stock.currentPrice}원 ({formatPriceChange(stock.previousDayDiff)}) <span className={`change-rate ${Number(stock.changeRate) >= 0 ? 'positive' : 'negative'}`}>{formatChangeRate(stock.changeRate)}</span>
                    </div>
                      <div className="stock-volume">시가총액: {formatKoreanWon(stock.marketCap)}</div>
                  </div>
                ))}
              </div>
            )}
          </section>
        );
      default:
        return null;
    }
  };

  return (
    <div className="App">
      <header className="app-header">
        <h1>
          <span className="header-icon">▲</span>
          한국증시 실시간 정보
        </h1>
      </header>

      {/* 주요 지수 - 항상 표시 */}
      <section className="stock-index-section">
        <h2>
          <span className="section-icon">■</span>
          주요 지수
        </h2>
        <div className="indices-container">
          {indices.map((index, idx) => {
            // 지수 코드 매핑 (한투 API 코드)
            const indexCodeMap: { [key: string]: string } = {
              'KOSPI': '0001',
              'KOSDAQ': '1001',
              'KOSPI200': '2001'
            };
            const indexCode = indexCodeMap[index.stockName || ''];

            return (
              <div
                key={idx}
                className={`index-card ${updatedIndices.has(idx) ? 'updated' : ''}`}
                onClick={() => indexCode && handleStockClick(indexCode, index.stockName)}
                style={{ cursor: indexCode ? 'pointer' : 'default' }}
              >
                <div className="index-name">{index.stockName}</div>
                <div className="index-value">{index.indexValue}</div>
                <div className={`index-change ${index.indexChange?.includes('-') ? 'negative' : 'positive'}`}>
                  {index.indexChange} ({index.changeRate}%)
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* 탭 네비게이션 */}
      <nav className="tab-navigation">
        <button
          className={`tab-button ${activeTab === 'gainers' ? 'active' : ''}`}
          onClick={() => setActiveTab('gainers')}
        >
          <span className="tab-icon rise">▲</span> 상승률 TOP
        </button>
        <button
          className={`tab-button ${activeTab === 'losers' ? 'active' : ''}`}
          onClick={() => setActiveTab('losers')}
        >
          <span className="tab-icon fall">▼</span> 하락률 TOP
        </button>
        <button
          className={`tab-button ${activeTab === 'volume' ? 'active' : ''}`}
          onClick={() => setActiveTab('volume')}
        >
          <span className="tab-icon volume">▐</span> 거래량 TOP
        </button>
        <button
          className={`tab-button ${activeTab === 'marketcap' ? 'active' : ''}`}
          onClick={() => setActiveTab('marketcap')}
        >
          <span className="tab-icon market">●</span> 시가총액 TOP
        </button>
      </nav>

      {/* 탭 컨텐츠 */}
      <main className="main-content">
        {renderTabContent()}
      </main>

      {/* 차트 모달 */}
      {selectedStock && (
        <StockChart
          stockCode={selectedStock.code}
          stockName={selectedStock.name}
          onClose={handleCloseChart}
        />
      )}
    </div>
  );
}

export default App;
