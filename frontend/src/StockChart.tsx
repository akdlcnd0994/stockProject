import React, { useEffect, useState } from 'react';
import {
  ComposedChart,
  Line,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import './StockChart.css';

interface CandleData {
  date: string;
  open: string;
  high: string;
  low: string;
  close: string;
  volume: string;
  changeRate: string;
}

interface ChartData {
  stockCode: string;
  stockName: string;
  timeframe: string;
  candles: CandleData[];
}

interface StockChartProps {
  stockCode: string;
  stockName: string;
  onClose: () => void;
}

function StockChart({ stockCode, stockName, onClose }: StockChartProps) {
  const [timeframe, setTimeframe] = useState<string>('D');
  const [chartData, setChartData] = useState<ChartData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // 차트 데이터 가져오기
  const fetchChartData = async (tf: string) => {
    setLoading(true);
    try {
      const response = await fetch(`http://localhost:8080/api/stock/chart/${stockCode}?timeframe=${tf}`);
      const data: ChartData = await response.json();
      setChartData(data);
    } catch (error) {
      console.error('차트 데이터 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  // 타임프레임 변경 시 데이터 재조회
  useEffect(() => {
    fetchChartData(timeframe);
  }, [timeframe, stockCode]);

  // Recharts용 데이터 변환
  const getChartDataForRecharts = () => {
    if (!chartData || !chartData.candles) return [];

    // 데이터를 뒤집어서 최신 데이터가 오른쪽에 오도록 함 (과거 <- 최신)
    return chartData.candles.slice().reverse().map(candle => ({
      date: `${candle.date.substring(4, 6)}/${candle.date.substring(6, 8)}`,
      fullDate: candle.date,
      open: parseFloat(candle.open),
      high: parseFloat(candle.high),
      low: parseFloat(candle.low),
      close: parseFloat(candle.close),
      volume: parseFloat(candle.volume),
      changeRate: parseFloat(candle.changeRate),
    }));
  };

  const data = getChartDataForRecharts();

  // 커스텀 툴팁
  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="custom-tooltip">
          <p className="label">{`날짜: ${data.fullDate}`}</p>
          <p className="open">{`시가: ${data.open.toLocaleString()}원`}</p>
          <p className="high">{`고가: ${data.high.toLocaleString()}원`}</p>
          <p className="low">{`저가: ${data.low.toLocaleString()}원`}</p>
          <p className="close">{`종가: ${data.close.toLocaleString()}원`}</p>
          <p className="volume">{`거래량: ${data.volume.toLocaleString()}`}</p>
          {/*<p className={`rate ${data.changeRate >= 0 ? 'positive' : 'negative'}`}>*/}
          {/*  {`등락률: ${data.changeRate >= 0 ? '+' : ''}${data.changeRate}%`}*/}
          {/*</p>*/}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="chart-modal-overlay" onClick={onClose}>
      <div className="chart-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="chart-header">
          <h2>
            <span className="chart-icon">▲</span>
            {stockName} ({stockCode})
          </h2>
          <button className="close-button" onClick={onClose}>✕</button>
        </div>

        {/* 타임프레임 선택 버튼 */}
        <div className="timeframe-buttons">
          <button
            className={`timeframe-btn ${timeframe === 'D' ? 'active' : ''}`}
            onClick={() => setTimeframe('D')}
          >
            일봉
          </button>
          <button
            className={`timeframe-btn ${timeframe === 'W' ? 'active' : ''}`}
            onClick={() => setTimeframe('W')}
          >
            주봉
          </button>
          <button
            className={`timeframe-btn ${timeframe === 'M' ? 'active' : ''}`}
            onClick={() => setTimeframe('M')}
          >
            월봉
          </button>
        </div>

        {/* 차트 */}
        {loading ? (
          <div className="chart-loading">차트 데이터를 불러오는 중...</div>
        ) : data.length > 0 ? (
          <div className="chart-wrapper">
            {/* 가격 차트 */}
            <ResponsiveContainer width="100%" height={400}>
              <ComposedChart
                data={data}
                margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#ecf0f1" />
                <XAxis
                  dataKey="date"
                  stroke="#7f8c8d"
                  tick={{ fill: '#7f8c8d', fontSize: 12 }}
                  axisLine={{ stroke: '#bdc3c7' }}
                />
                <YAxis
                  domain={['auto', 'auto']}
                  stroke="#7f8c8d"
                  tick={{ fill: '#7f8c8d', fontSize: 12 }}
                  axisLine={{ stroke: '#bdc3c7' }}
                  tickFormatter={(value) => value.toLocaleString()}
                />
                <Tooltip content={<CustomTooltip />} />
                <Legend
                  wrapperStyle={{ fontSize: '13px', fontWeight: 500, color: '#7f8c8d' }}
                />
                <Line
                  type="monotone"
                  dataKey="close"
                  stroke="#2c3e50"
                  strokeWidth={2.5}
                  dot={false}
                  name="종가"
                />
                <Line
                  type="monotone"
                  dataKey="high"
                  stroke="#e74c3c"
                  strokeWidth={1.5}
                  strokeDasharray="5 5"
                  dot={false}
                  name="고가"
                />
                <Line
                  type="monotone"
                  dataKey="low"
                  stroke="#3498db"
                  strokeWidth={1.5}
                  strokeDasharray="5 5"
                  dot={false}
                  name="저가"
                />
              </ComposedChart>
            </ResponsiveContainer>

            {/* 거래량 차트 */}
            <ResponsiveContainer width="100%" height={150}>
              <ComposedChart
                data={data}
                margin={{ top: 10, right: 30, left: 20, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="#ecf0f1" />
                <XAxis
                  dataKey="date"
                  stroke="#7f8c8d"
                  tick={{ fill: '#7f8c8d', fontSize: 12 }}
                  axisLine={{ stroke: '#bdc3c7' }}
                />
                <YAxis
                  stroke="#7f8c8d"
                  tick={{ fill: '#7f8c8d', fontSize: 12 }}
                  axisLine={{ stroke: '#bdc3c7' }}
                  tickFormatter={(value) => (value / 1000).toFixed(0) + 'K'}
                />
                <Tooltip
                  formatter={(value: any) => value.toLocaleString()}
                  labelFormatter={(label) => `날짜: ${label}`}
                  contentStyle={{
                    border: '2px solid #3498db',
                    borderRadius: '6px',
                    backgroundColor: 'white'
                  }}
                />
                <Bar dataKey="volume" fill="#27ae60" name="거래량" opacity={0.7} />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="chart-loading">데이터가 없습니다.</div>
        )}
      </div>
    </div>
  );
}

export default StockChart;
